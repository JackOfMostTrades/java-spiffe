package io.spiffe.workloadapi;

import com.google.common.collect.Sets;
import io.spiffe.exception.SocketEndpointAddressException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static io.spiffe.workloadapi.AddressScheme.TCP_SCHEME;
import static io.spiffe.workloadapi.AddressScheme.UNIX_SCHEME;

/**
 * Utility class to get the default Workload API address and parse string addresses.
 */
public class Address {

    /**
     * Environment variable holding the default Workload API address.
     */
    public static final String SOCKET_ENV_VARIABLE = "SPIFFE_ENDPOINT_SOCKET";

    private static final Set<AddressScheme> VALID_SCHEMES = Sets.newHashSet(UNIX_SCHEME, TCP_SCHEME);

    private Address() {
    }

    /**
     * @return the default Workload API address hold by the system environment variable
     * defined by SOCKET_ENV_VARIABLE.
     */
    public static String getDefaultAddress() {
        return System.getenv(Address.SOCKET_ENV_VARIABLE);
    }

    /**
     * Parses and validates a Workload API socket address.
     * <p>
     * The value of the address is structured as an RFC 3986 URI. The scheme MUST be set to either unix or tcp,
     * which indicates that the endpoint is served over a Unix Domain Socket or a TCP listen socket, respectively.
     * <p>
     * If the scheme is set to unix, then the authority component MUST NOT be set, and the path component MUST be set
     * to the absolute path of the SPIFFE Workload Endpoint Unix Domain Socket (e.g. unix:///path/to/endpoint.sock).
     * The scheme and path components are mandatory, and no other component may be set.
     * <p>
     * If the scheme is set to tcp, then the host component of the authority MUST be set to an IP address,
     * and the port component of the authority MUST be set to the TCP port number of the
     * SPIFFE Workload Endpoint TCP listen socket.
     * The scheme, host, and port components are mandatory, and no other component may be set.
     * As an example, tcp://127.0.0.1:8000 is valid, and tcp://127.0.0.1:8000/foo is not.
     *
     * @param address the Workload API socket address as a string
     * @return an instance of a {@link URI}
     * @throws SocketEndpointAddressException if the address could not be parsed or if it doesn't complain to the rules
     *                                        defined in the SPIFFE Worload Endpoint Standard.
     * @see <a href="https://github.com/spiffe/spiffe/blob/master/standards/SPIFFE_Workload_Endpoint.md#4-locating-the-endpoint">SPIFFE Workload Endpoint Standard</a>
     */
    public static URI parseAddress(final String address) throws SocketEndpointAddressException {

        val parsedAddress = parseUri(address);
        val scheme = getScheme(parsedAddress);

        if (scheme == UNIX_SCHEME) {
            validateUnixAddress(parsedAddress);
        } else {
            validateTcpAddress(parsedAddress);
        }

        return parsedAddress;
    }

    private static URI parseUri(final String address) throws SocketEndpointAddressException {
        final URI parsedAddress;
        try {
            parsedAddress = new URI(address);
        } catch (URISyntaxException e) {
            val error = "Workload endpoint socket is not a valid URI: %s";
            throw new SocketEndpointAddressException(String.format(error, address), e);
        }
        return parsedAddress;
    }

    private static AddressScheme getScheme(final URI parsedAddress) throws SocketEndpointAddressException {
        try {
            val scheme = parsedAddress.getScheme();
            return AddressScheme.parseScheme(scheme);
        } catch (IllegalArgumentException e) {
            val error = "Workload endpoint socket URI must have a tcp:// or unix:// scheme: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress.toString()));
        }
    }

    private static void validateUnixAddress(final URI parsedAddress) throws SocketEndpointAddressException {
        if (parsedAddress.isOpaque()) {
            val error = "Workload endpoint unix socket URI must not be opaque: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (StringUtils.isNotBlank(parsedAddress.getRawAuthority())) {
            val error = "Workload endpoint unix socket URI must not include authority component: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (hasEmptyPath(parsedAddress.getPath())) {
            val error = "Workload endpoint unix socket path cannot be blank: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (StringUtils.isNotBlank(parsedAddress.getRawQuery())) {
            val error = "Workload endpoint unix socket URI must not include query values: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (StringUtils.isNotBlank(parsedAddress.getFragment())) {
            val error = "Workload endpoint unix socket URI must not include a fragment: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }
    }

    private static void validateTcpAddress(final URI parsedAddress) throws SocketEndpointAddressException {
        if (parsedAddress.isOpaque()) {
            val error = "Workload endpoint tcp socket URI must not be opaque: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (StringUtils.isNotBlank(parsedAddress.getUserInfo())) {
            val error = "Workload endpoint tcp socket URI must not include user info: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (StringUtils.isBlank(parsedAddress.getHost())) {
            final String error = "Workload endpoint tcp socket URI must include a host: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (StringUtils.isNotBlank(parsedAddress.getPath())) {
            val error = "Workload endpoint tcp socket URI must not include a path: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (StringUtils.isNotBlank(parsedAddress.getRawQuery())) {
            val error = "Workload endpoint tcp socket URI must not include query values: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        if (StringUtils.isNotBlank(parsedAddress.getFragment())) {
            val error = "Workload endpoint tcp socket URI must not include a fragment: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        val ipValid = InetAddressValidator.getInstance().isValid(parsedAddress.getHost());
        if (!ipValid) {
            val error = "Workload endpoint tcp socket URI host component must be an IP:port: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }

        int port = parsedAddress.getPort();
        if (port == -1) {
            final String error = "Workload endpoint tcp socket URI host component must include a port: %s";
            throw new SocketEndpointAddressException(String.format(error, parsedAddress));
        }
    }

    private static boolean hasEmptyPath(final String path) {
        return StringUtils.isBlank(path) || "/".equals(path);
    }

    private static boolean isSchemeNotValid(final String scheme) {
        return !VALID_SCHEMES.contains(scheme);
    }
}
